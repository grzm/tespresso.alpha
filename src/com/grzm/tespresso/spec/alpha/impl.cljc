(ns com.grzm.tespresso.spec.alpha.impl
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as test]
   [clojure.string :as str]))

;; extracted from clojure.spec.test.alpha
(defn failure-type [x] (::s/failure (ex-data x)))
(defn unwrap-failure [x] (if (failure-type x) (ex-data x) x))
(defn failure? [{:keys [:failure]}] (not (or (true? failure) (nil? failure))))

;; modified from clojure.spec.test.alpha
(defn abbrev-result [x ret-key]
  (let [failure (:failure x)]
    (if (failure? x)
      (-> (dissoc x ret-key)
          (update :spec s/describe)
          (update :failure unwrap-failure))
      (dissoc x :spec ret-key))))

(defn throwable? [x]
  #?(:clj (instance? Throwable x)
     :cljs (instance? js/Error x)))

(defn failure-report [failure ret-key]
  (let [expected (->> (abbrev-result failure ret-key)
                      :spec
                      rest
                      (apply hash-map)
                      :ret)]
    (if (throwable? failure)
      {:type     :error
       :message  "Exception thrown in check"
       :expected expected
       :actual   failure}
      (let [data (ex-data (get-in failure
                                  [ret-key
                                   :result-data
                                   :clojure.test.check.properties/error]))]
        {:type     :fail
         :message  (with-out-str (s/explain-out data))
         :expected expected
         :actual   (::s/value data)}))))

(defn result-reports [results ret-key]
  (let [failures (filter failure? results)]
    (if (empty? failures)
      [{:type    :pass
        :message (str "Generative tests pass for "
                      (str/join ", " (map :sym results)))}]
      (map #(failure-report % ret-key) failures))))

(defn passed?
  [reports]
  (empty? (filter #(= :fail (:type %)) reports)))

(defn check?
  [msg [_ body :as form] ret-key]
  `(let [results# ~body]
     (result-reports results# ~ret-key)))

#?(:clj
   (defn check-assert
     [sym-or-syms opts ret-key]
     (fn []
       (let [results (stest/check sym-or-syms opts)
             reports (result-reports results ret-key)]
         (dorun (map test/do-report reports))
         (passed? reports)))))
