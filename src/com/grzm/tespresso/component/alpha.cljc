(ns com.grzm.tespresso.component.alpha
  (:require
   [com.stuartsierra.component :as component]))

(defmacro with-system
  "binding => [name init-value]

  Sets name to an atom containing the initial value of the system.
  In a try expression, calls (component/start @name) and evaluates
  the body, and finally calls (component/stop @name) to shut down
  the system."
  [[sym initial-value :as binding] & body]
  `(let [~sym (atom ~initial-value)]
     (try
       (swap! ~sym component/start)
       ~@body
       (finally (component/stop @~sym)))))
