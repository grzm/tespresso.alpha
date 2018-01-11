(ns com.grzm.tespresso.alpha.cljs
  (:require
   [cljs.test :as test]
   [com.grzm.tespresso.alpha.impl :as impl]))

#?(:clj
   (try
     (require 'cljs.test
              '[com.grzm.tespresso.alpha.impl :as impl])
     (eval
       '(defmethod cljs.test/assert-expr 'com.grzm.tespresso/lines-match?
          [_ msg form]
          `(test/do-report ~(impl/lines-match? msg form))))
     (eval
       '(defmethod cljs.test/assert-expr 'com.grzm.tespresso/thrown-with-data?
          [_ msg form]
          `(dorun (map test/do-report ~(impl/thrown-with-data? msg form 'cljs.core/ExceptionInfo)))))
     (catch java.io.FileNotFoundException e)))
