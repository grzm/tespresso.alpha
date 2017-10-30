(ns com.grzm.tespresso.alpha.test
  (:require
   [clojure.test :as test]))

(declare ^:dynamic test-report)

(defn capturing-report [reports m]
  (swap! reports conj m)
  (test-report m))

(defn capture-test-var
  "Returns map of :reports, :report-counters, :out, and :test-out."
  [v]
  (let [reports (atom [])]
    (binding [test-report test/report
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

