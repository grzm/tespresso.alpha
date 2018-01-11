(ns com.grzm.tespresso.spec.alpha.cljs
  (:require
   [cljs.test :as test]
   [cljs.analyzer :as ana]
   [com.grzm.tespresso.spec.alpha.impl :as impl]))

#?(:clj
   (defmethod cljs.test/assert-expr 'com.grzm.tespresso.spec/check?
     [_ msg form]
     `(dorun (map test/do-report ~(impl/check? msg form :clojure.test.check/ret)))))
