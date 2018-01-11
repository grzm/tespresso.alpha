(ns com.grzm.tespresso.component-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.component.alpha :refer [with-system]]
   [com.stuartsierra.component :as component]))

(defrecord App []
  component/Lifecycle
  (start [this] (assoc this :state :started))
  (stop [this] this))

(def system-map (component/system-map :app (->App)))

(deftest with-system-test
  (with-system [system system-map]
    (is (= [:app] (keys @system)))
    (is (= :started (-> @system :app :state)))))
