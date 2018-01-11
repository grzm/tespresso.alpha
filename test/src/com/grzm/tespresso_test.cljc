(ns com.grzm.tespresso-test
  (:require
   [clojure.string :as str]
   #?(:clj [clojure.test :as test :refer [deftest is are]]
      :cljs [cljs.test :as test :include-macros true :refer [deftest is are]])
   [com.grzm.tespresso.alpha :as tespresso :refer [capture-test-var]]))

(defn exclude-meta
  "Helper for filtering vars based on meta. Used for boot-alt-test"
  [tag t]
  (not (get (meta t) tag)))

(def exclude-capture
  "Filter function for `boot-alt-test` to exclude tests with ::capture metadata.
  Used for boot-alt-test"
  (partial exclude-meta ::capture))

(deftest lines-match?
  (is (com.grzm.tespresso/lines-match?
        ["line 1"
         #"line \d"
         #"line 3"]
        "line 1\nline 2\nline 3")))

(deftest ex-data=
  (let [x {:a 1, :b 2}]
    (is ((tespresso/ex-data= {:a 1, :b 2}) x))
    (is (false? ((tespresso/ex-data= {:a 1}) x)))))

(deftest ex-data-select-keys
  (let [x {:a 1, :b 2}]
    (is ((tespresso/ex-data-select= {:a 1}) x))
    (is (false? ((tespresso/ex-data-select= {:b 1}) x)))
    (is (false? ((tespresso/ex-data-select= {:c 1}) x)))))

;; there's an issue with reloading skipped vars
;; editting a skipped deftest var can result in NPE with boot-alt-test

(deftest thrown-with-ex-data=
  (is (com.grzm.tespresso/thrown-with-data?
        #"Some"
        (tespresso/ex-data= {:cause ::example-thrower, :a 1})
        (throw (ex-info "Something"
                        {:cause ::example-thrower, :a 1})))))

(deftest thrown-with-ex-data-select=
  (is (com.grzm.tespresso/thrown-with-data?
        #"Some .* thing"
        (tespresso/ex-data-select= {:cause ::example-thrower})
        (throw (ex-info "Some other thing"
                        {:cause ::example-thrower, :a 1})))))

(deftest thrown-with-data-but-no-message
  (is (com.grzm.tespresso/thrown-with-data?
        #(= ::example-thrower (:cause %))
        (throw (ex-info "Ignored message"
                        {:cause ::example-thrower})))))

(deftest ^::capture thrown-with-data-example
  (let [my-data {:a 1, :b 2}]
    (is (com.grzm.tespresso/thrown-with-data?
          #"."
          (tespresso/ex-data-select= {:cause ::example-thrower, :a 1})
          (throw (ex-info "something"
                          {:a 1}))))))

(deftest thrown-with-data?
  (let [{:keys [test-out]} (capture-test-var #'thrown-with-data-example)]
    (is (com.grzm.tespresso/lines-match?
          [#"FAIL in \(thrown-with-data-example\)"
           #"."
           #"."
           #"."]
          test-out))))

(deftest ^::capture thrown-with-data-msg-failure-example
  (is (com.grzm.tespresso/thrown-with-data?
        #"wrong message"
        (tespresso/ex-data= {:cause ::self-inflicted})
        (throw (ex-info "right message"
                        {:cause ::self-inflicted})))))

(deftest thrown-with-data-msg-failure
  (let [{:keys [test-out]} (capture-test-var
                             #'thrown-with-data-msg-failure-example)]
    (is (com.grzm.tespresso/lines-match?
          [#"^FAIL in \(thrown-with-data-msg-failure-example\)"
           "message doesn't match"
           #"^expected: #\"wrong message\""
           #"^  actual: \"right message\""]
          test-out))))

(deftest ^::capture thrown-with-data-data-failure-example
  (is (com.grzm.tespresso/thrown-with-data?
        #"right message"
        (tespresso/ex-data= {:cause :unexpected})
        (throw (ex-info "right message"
                        {:cause :self-inflicted})))))

(deftest thrown-with-data-data-failure
  (let [{:keys [test-out]} (capture-test-var
                             #'thrown-with-data-data-failure-example)]
    (is (com.grzm.tespresso/lines-match?
          [#"^FAIL in \(thrown-with-data-data-failure-example\)"
           "data doesn't match"
           #"^expected: \(tespresso/ex-data= \{:cause :unexpected\}\)"
           #"^  actual: \{:cause :self-inflicted\}"]
          test-out))))

(deftest ^::capture thrown-with-data-msg-and-data-failure-example
  (is (com.grzm.tespresso/thrown-with-data?
        #"wrong message"
        (tespresso/ex-data= {:cause :unexpected})
        (throw (ex-info "right message"
                        {:cause :self-inflicted})))))

(deftest thrown-with-data-msg-and-data-failure
  (let [{:keys [test-out]} (capture-test-var
                             #'thrown-with-data-msg-and-data-failure-example)]
    (is (com.grzm.tespresso/lines-match?
          [#"^FAIL in \(thrown-with-data-msg-and-data-failure-example\)"
           "message doesn't match"
           #"^expected: #\"wrong message\""
           #"^  actual: \"right message\""
           ""
           #"^FAIL in \(thrown-with-data-msg-and-data-failure-example\)"
           "data doesn't match"
           #"^expected: \(tespresso/ex-data= \{:cause :unexpected\}\)"
           #"^  actual: \{:cause :self-inflicted\}"]
          test-out))))

(deftest ^::capture thrown-with-data-no-msg-data-failure-example
  (is (com.grzm.tespresso/thrown-with-data?
        (tespresso/ex-data= {:cause :unexpected})
        (throw (ex-info "ignored message"
                        {:cause :self-inflicted})))))

(deftest thrown-with-data-no-msg-data-failure
  (let [{:keys [test-out]} (capture-test-var
                             #'thrown-with-data-no-msg-data-failure-example)]
    (is (com.grzm.tespresso/lines-match?
          [#"^FAIL in \(thrown-with-data-no-msg-data-failure-example\)"
           "data doesn't match"
           #"^expected: \(tespresso/ex-data= \{:cause :unexpected\}\)"
           #"^  actual: \{:cause :self-inflicted\}"]
          test-out))))


(deftest ^::capture inequality-failure
  (is (= 1 2)))

(deftest test-inequality-failure-capture
  (let [{:keys [test-out]} (capture-test-var #'inequality-failure)]
    (is (com.grzm.tespresso/lines-match?
          [#?(:clj  #"FAIL in \(inequality-failure\) \(tespresso_test\.cljc:\d+\)"
              :cljs #"FAIL in \(inequality-failure\) \(.*\.js:\d+:\d+\)")
           "expected: (= 1 2)"
           "  actual: (not (= 1 2))"]
          test-out))))

(defn test-ns-hook
  "Ignore tests with ::capture metadata."
  []
  (tespresso/test-ns-interns-sans-meta-key
    (ns-interns 'com.grzm.tespresso-test) ::capture))
