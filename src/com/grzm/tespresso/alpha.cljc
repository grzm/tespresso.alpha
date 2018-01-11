(ns com.grzm.tespresso.alpha
  #?(:cljs (:require-macros [com.grzm.tespresso.alpha.cljs]))
  (:require
   [clojure.string :as str]
   #?(:clj [clojure.test :as test]
      :cljs [cljs.test :as test :include-macros true])
   [com.grzm.tespresso.alpha.impl :as impl])
  #?(:clj
     (:import
      (clojure.lang ExceptionInfo))))

(defn ex-data-select=
  [m]
  #(= m (select-keys % (keys m))))

(defn ex-data=
  [m]
  #(= m %))

#?(:clj
   (defmethod test/assert-expr 'com.grzm.tespresso/thrown-with-data?
     [msg form]
     `(dorun
        (map test/do-report
             ~(impl/thrown-with-data? msg form 'clojure.lang.ExceptionInfo))))
   :cljs
   (when (exists? js/cljs.test$macros)
     (defmethod js/cljs.test$macros.assert_expr 'com.grzm.tespresso/thrown-with-data?
       [_ msg form]
       `(dorun
          (map test/do-report
               ~(impl/thrown-with-data? msg form 'cljs.core/ExceptionInfo))))))

#?(:clj
   (defmethod test/assert-expr 'com.grzm.tespresso/lines-match?
     [msg form]
     `(test/do-report ~(impl/lines-match? msg form)))
   :cljs
   (when (exists? js/cljs.test$macros)
     (defmethod js/cljs.test$macros.assert_expr 'com.grzm.tespresso/lines-match?
       [_ msg form]
       `(test/do-report ~(impl/lines-match? msg form)))))

(declare ^:dynamic *report*)

(defn capturing-report [reports m]
  (swap! reports conj m)
  (*report* m))

(defn capture-test-var
  "Returns map of :reports, :report-counters, :out, and :test-out."
  [v]
  (let [reports (atom [])]
    (binding [*report*    test/report
              test/report (partial capturing-report reports)]
      #?(:clj
         (binding [test/*report-counters*  (ref test/*initial-report-counters*)
                   test/*test-out*         (java.io.StringWriter.)
                   test/*testing-contexts* (list)
                   test/*testing-vars*     (list)]
           (let [out (with-out-str (test/test-var v))]
             {:reports         @reports
              :report-counters @test/*report-counters*
              :out             out
              :test-out        (str test/*test-out*)}))
         :cljs
         (binding [test/*current-env* (test/empty-env)]
           (let [out (with-out-str (test/test-var v))]
             ;; cljs.test doesn't distinguish between *out* and *test-out*
             {:reports         @reports
              :report-counters (:report-counters test/*current-env*)
              :out             out
              :test-out        out}))))))

(defn test-ns-interns-sans-meta-key [interns k]
  (test/test-vars (impl/tests-sans-meta-key interns k)))
