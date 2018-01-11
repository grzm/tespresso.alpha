(ns com.grzm.tespresso.tools-logging.alpha
  (:require
   [clojure.tools.logging :as log]
   [clojure.tools.logging.impl :as impl]))

(defn test-factory [enabled-levels entries]
  (reify impl/LoggerFactory
    (name [_] "test factory")
    (get-logger [_ ns]
      (reify impl/Logger
        (enabled? [_ level] (contains? enabled-levels level))
        (write! [_ level throwable msg]
          (swap! entries conj [(str ns) level throwable msg]))))))

(defmacro with-logging
  [[enabled-levels entries] & body]
  (let [enabled-levels (or enabled-levels #{:trace :debug :info :warn :error :fatal})
        entries     (or entries 'log-entries)]
    `(let [~entries   (atom [])]
       (binding [log/*logger-factory* (test-factory
                                        ~enabled-levels
                                        ~entries)]
         ~@body))))
