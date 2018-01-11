(ns com.grzm.tespresso.bytes.alpha
  (:require
   [byte-streams :as bs]
   [clojure.test :as test]
   [com.grzm.tespresso.bytes.alpha.impl :as impl])
  (:import
   (java.nio ByteBuffer)))

(defn byte-buffer
  "Converts a sequence of integers representing bytes to a ByteBuffer"
  [byte-seq]
  (-> byte-seq byte-array (bs/convert ByteBuffer)))

(defmethod test/assert-expr 'com.grzm.tespresso.bytes/bytes=
   [msg [_ expected actual :as form]]
  `(test/report ~(impl/bytes=* msg expected actual)))
