(ns com.grzm.tespresso.bytes.alpha.impl
  (:require
   [byte-streams :as bs]
   [clojure.test :as test])
  (:import
   (java.nio ByteBuffer)))

(let [special-character? (->> "' _-+=`~{}[]()\\/#@!?.,;\"" (map int) set)]
  (defn- readable-character? [x]
    (or
      (Character/isLetterOrDigit (int x))
      (special-character? (int x)))))

(defn format-bytes [buf]
  (let [s          (bs/convert (.duplicate buf) String {:encoding "ISO-8859-1"})
        bytes      (repeatedly (min 16 (.remaining buf)) #(.get buf))
        padding    (* 3 (- 16 (count bytes)))
        hex-format #(->> "%02X" (repeat %) (interpose " ") (apply str))]
    [(apply format
            (str (hex-format (min 8 (count bytes)))
                 "  "
                 (hex-format (max 0 (- (count bytes) 8))))
            bytes)
     (->> s (map #(if (readable-character? %) % ".")) (apply str))]))

(defn gather-bytes [bytes]
  (let [bufs (bs/convert bytes (bs/seq-of ByteBuffer) {:chunk-size 16})]
    (vec (mapcat format-bytes bufs))))

(defn bytes=*
  [msg expected actual]
  `(let [passed?# (bs/bytes= ~expected ~actual)]
     (if passed?#
       {:type    :pass
        :message ~msg}
       {:type     :fail
        :message  ~msg
        :expected (gather-bytes ~expected)
        :actual   (gather-bytes ~actual)})))
