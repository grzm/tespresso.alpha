(ns com.grzm.tespresso.tools-logging-test
  (:require
   [clojure.test :refer [deftest is]]
   [clojure.tools.logging :as log]
   [com.grzm.tespresso.tools-logging.alpha :refer [with-logging]]))

(deftest test-logging
  (with-logging [#{:info :warn} log-entries]
    (log/tracef "Some trace message")
    (log/infof "Some info message")
    (log/warnf "Some example warning")
    (is (= [["com.grzm.tespresso.tools-logging-test" :info nil "Some info message"]
            ["com.grzm.tespresso.tools-logging-test" :warn nil "Some example warning"]]
           @log-entries))))
