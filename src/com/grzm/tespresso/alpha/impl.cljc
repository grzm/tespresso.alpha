(ns com.grzm.tespresso.alpha.impl
  (:require
   [clojure.string :as str])
  #?(:clj
     (:import (clojure.lang ExceptionInfo))))

(defn regex?
  [x]
  #?(:clj (instance? java.util.regex.Pattern x)
     :cljs (instance? js/RegExp x)))

(defn matches?
  [matcher line]
  (cond
    (regex? matcher)
    (re-find matcher line)

    (string? matcher)
    (= matcher line)

    :else
    (throw (ex-info "Matcher should be string or regex"
                    {:matcher matcher}))))

(defn lines-match?
  [msg [_ matchers s :as form]]
  `(let [lines#     (-> ~s str/trim str/split-lines)
         has-rest?# (= (last ~matchers) :rest)
         matchers#  (if has-rest?#
                      (butlast ~matchers)
                      ~matchers)
         lines#     (if has-rest?#
                      (take (count matchers#) lines#)
                      lines#)]
     (if (not= (count matchers#) (count lines#))
       {:type     :fail
        :message  ~msg
        :expected '~form
        :actual   {:cause         ::wrong-number-of-lines
                   :lines         lines#
                   :matchers      ~matchers
                   :line-count    (count lines#)
                   :matcher-count ~(count matchers)}}
       (loop [rows# (map vector (map inc (range)) matchers# lines#)]
         (if (seq rows#)
           (let [[no# matcher# line#] (first rows#)]
             (if (matches? matcher# line#)
               (recur (next rows#))
               {:type     :fail
                :message  ~msg
                :expected '~form
                :actual   {:cause              ::lines-failed-match
                           :lines              lines#
                           :matchers           ~matchers
                           :failed-line        line#
                           :failed-matcher     matcher#
                           :failed-line-number no#}}))
           {:type     :pass
            :message  ~msg
            :expected '~form
            :actual   '~form})))))

(defn error-message [e]
  #?(:clj (.getMessage e)
     :cljs (.-message e)))

(defn thrown-with-data?
  [msg form klass]
  (let [match-message?    (regex? (second form))
        [re data-fn body] (if match-message?
                            [(nth form 1) (nth form 2) (drop 3 form)]
                            [nil (nth form 1) (drop 2 form)])]
    `(try ~@body
          [{:type     :fail
            :message  (str (when ~msg (str ~msg ": "))
                           "expected exception")
            :expected '~form
            :actual   nil}]
          (catch ~klass e#
            (let [m# (error-message e#)
                  d# (ex-data e#)

                  failures#
                  (cond-> []
                    (and ~re (not (re-find ~re m#)))
                    (conj {:type     :fail,
                           :message  (str (when ~msg (str ~msg ": "))
                                          "message doesn't match")
                           :expected '~re,
                           :actual   m#})

                    (not (~data-fn d#))
                    (conj {:type     :fail
                           :message  (str (when ~msg (str ~msg ": "))
                                          "data doesn't match")
                           :expected '~data-fn
                           :actual   d#}))]

              (if (empty? failures#)
                [{:type     :pass
                  :message  ~msg
                  :expected '~form
                  :actual   e#}]
                failures#))))))

(defn tests-sans-meta-key [interns k]
  (->> (vals interns)
       (filter #(let [m (meta %)]
                  (and (:test m)
                       (not (k m)))))
       (sort-by #(:line (meta %)))))
