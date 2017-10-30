(ns com.grzm.tespresso.alpha
  (:require
   [com.grzm.tespresso.alpha.bytes :as bytes]
   [com.grzm.tespresso.alpha.spec :as spec]
   [com.grzm.tespresso.alpha.component :as component]
   #?@(:cljs
       [[cljs.test :as test :include-macros true
         :refer-macros [is deftest testing]]])
   #?(:clj
      [clojure.test :as test]))
  (:import
   (clojure.lang ExceptionInfo)))

(defmethod test/assert-expr 'com.grzm.tespresso/thrown-with-data?
  [msg form]
  (let [re              (nth form 1)
        data-matches-fn (nth form 2)
        body            (nthnext form 3)]
    `(try ~@body
          (test/do-report {:type     :fail
                           :message  (str ~msg ": expected exception")
                           :expected '~form
                           :actual   nil})
          (catch ExceptionInfo e#
            (let [m# (.getMessage e#)
                  d# (ex-data e#)]
              (if-not (re-find ~re m#)
                (test/do-report {:type     :fail,
                                 :message  (str ~msg ": message doesn't match")
                                 :expected '~form,
                                 :actual   e#})
                (if (~data-matches-fn d#)
                  (test/do-report {:type     :pass
                                   :message  ~msg
                                   :expected '~form
                                   :actual   e#})
                  (test/do-report {:type     :fail
                                   :message  (str ~msg ": data doesn't match")
                                   :expected '~data-matches-fn
                                   :actual   d#}))))
            e#))))


(defmacro defspec
  "Based on https://gist.github.com/kennyjwilli/8bf30478b8a2762d2d09baabc17e2f10"
  ([name sym-or-syms] `(defspec ~name ~sym-or-syms nil))
  ([name sym-or-syms opts]
   (when test/*load-tests*
     `(def ~(vary-meta
              name assoc
              :test `(fn []
                       (let [results#  (stest/check ~sym-or-syms ~opts)
                             passed?# (every? nil? (map :failure results#))]
                         (spec/report-results results#)
                         passed?#)))
        (fn [] (test/test-var (var ~name)))))))

(defmethod test/assert-expr 'com.grzm.tespresso.spec-test/check? [msg form]
  `(let [results# ~(second form)]
     (spec/report-results results#)))

(defn ex-data-keys= [m]
  #(= m (select-keys % (keys m))))

(defmethod test/assert-expr 'com.grzm.tespresso/bytes=
  [msg form]
  `(let [expected# ~(nth form 1)
         actual#   ~(nth form 2)]
     (bytes/bytes=* ~msg expected# actual#)))


(defmacro with-system
  "Wraps a test body and handles starting and stopping the system for the
   test. Wraps the test body in a try-finally block to ensure the system
   stops cleanly, isolating the test and allowing other tests to run."
  [system-var init-fn & body]
  (list 'do
        (list `component/go system-var init-fn)
        (list 'try (cons 'do body)
              (list 'finally (list `component/stop system-var)))))

(defmacro with-system-options
  "Wraps a test body and handles starting and stopping the system for the
   test. Wraps the test body in a try-finally block to ensure the system
   stops cleanly, isolating the test and allowing other tests to run."
  [system-var init-fn teardown-fn & body]
  (list 'do
        (list `component/go system-var init-fn)
        (list 'try (cons 'do body)
              (list 'finally
                    (list `component/stop system-var)
                    (list teardown-fn)))))
