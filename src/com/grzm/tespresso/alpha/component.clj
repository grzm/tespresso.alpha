(ns com.grzm.tespresso.alpha.component
  (:require
   [com.stuartsierra.component :as component]))

(defn- init [system-var init-fn]
  (alter-var-root system-var (init-fn)))

(defn- start [system-var]
  (alter-var-root system-var component/start))

(defn go [system-var init-fn]
  (do (init system-var init-fn)
      (start system-var)))

(defn stop [system-var]
  (alter-var-root system-var component/stop))

