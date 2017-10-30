(ns com.grzm.tespresso.alpha.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.string :as str]
   [clojure.test :as test]))

(defn report-results
  [results]
  (let [passed? (every? nil? (map :failure results))]
    (if passed?
      (test/do-report {:type    :pass
                       :message (str "Generative tests pass for "
                                     (str/join ", " (map :sym results)))})
      (doseq [failed-check (filter :failure results)
              :let         [r (stest/abbrev-result failed-check)
                            failure (:failure r)
                            expected (->> r :spec rest (apply hash-map) :ret)]]
        (if (instance? Throwable failure)
          (test/do-report
            {:type     :error
             :message  "Exception thrown in check"
             :expected expected
             :actual   failure})
          (test/do-report
            {:type     :fail
             :message  (with-out-str (s/explain-out failure))
             :expected expected
             :actual   (::s/val failure)}))))))
