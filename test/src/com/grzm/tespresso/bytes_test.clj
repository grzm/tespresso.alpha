(ns com.grzm.tespresso.bytes-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.alpha :as tespresso :refer [capture-test-var]]
   [com.grzm.tespresso.bytes.alpha :as bytes]))

(deftest ^::capture byte-equality-example
  (is (com.grzm.tespresso.bytes/bytes=
        (bytes/byte-buffer [0 0 0 16])
        (bytes/byte-buffer [0 0 0 16]))
      "Testing byte="))

(deftest test-byte=-success
  (let [{:keys [test-out reports]} (capture-test-var #'byte-equality-example)]
    (is (= "" test-out))
    (is (= [{:type :begin-test-var,
             :var #'com.grzm.tespresso.bytes-test/byte-equality-example}
            {:type :pass, :message "Testing byte="}
            {:type :end-test-var,
             :var #'com.grzm.tespresso.bytes-test/byte-equality-example}]
           reports))))

(deftest ^::capture byte-inequality-example
  (is (com.grzm.tespresso.bytes/bytes=
        (bytes/byte-buffer [0 1 (int \a) 3])
        (bytes/byte-buffer [0 2 4 3]))))

(deftest test-byte=-failure
  (let [{:keys [test-out]} (capture-test-var #'byte-inequality-example)]
    (is (com.grzm.tespresso/lines-match?
          ["FAIL in (byte-inequality-example) (:)"
           "expected: [\"00 01 61 03  \" \"..a.\"]"
           "  actual: [\"00 02 04 03  \" \"....\"]"]
          test-out))))

(defn test-ns-hook
  "Ignore tests with ::capture metadata."
  []
  (tespresso/test-ns-interns-sans-meta-key
    (ns-interns 'com.grzm.tespresso.bytes-test) ::capture))
