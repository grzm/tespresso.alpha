(ns com.grzm.tespresso.spec.alpha
  #?(:cljs (:require-macros [com.grzm.tespresso.spec.alpha.cljs]))
  (:require
   #?(:cljs [cljs.analyzer :as ana])
   [clojure.string :as str]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as test]
   [com.grzm.tespresso.spec.alpha.impl :as impl]))

#?(:clj
   (defmacro defcheck
     ([name sym-or-syms] `(defcheck ~name ~sym-or-syms nil))
     ([name sym-or-syms opts]
      (when test/*load-tests*
        `(def ~(vary-meta
                 name assoc
                 :test `(impl/check-assert ~sym-or-syms ~opts :clojure.spec.test.check/ret))
           (fn [] (test/test-var (var ~name))))))))

#?(:clj
   (defmethod test/assert-expr 'com.grzm.tespresso.spec/check?
     [msg form]
     `(dorun (map test/do-report ~(impl/check? msg form :clojure.spec.test.check/ret))))
   ;; :cljs
   ;; (when (exists? js/cljs.test$macros)
   ;;   (defmethod js/cljs.test$macros.assert_expr 'com.grzm.tespresso.spec/check?
   ;;     [_ msg form]
   ;;     (spec/check? msg form)))
   )
