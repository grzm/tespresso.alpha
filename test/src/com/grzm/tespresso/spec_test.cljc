(ns com.grzm.tespresso.spec-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.alpha :as tespresso :refer [capture-test-var]]
   [com.grzm.tespresso.spec.alpha :refer [defcheck]]))

(def stc-opts-key #?(:clj :clojure.spec.test.check/opts
                     :cljs :cljs.spec.test.check/opts))

(defn adder [a b]
  (+ a b))

(s/fdef adder
        :args (s/cat :a int? :b int?)
        :ret int?)

(deftest check-adder
  (is (com.grzm.tespresso.spec/check?
        (stest/check `adder {stc-opts-key {:num-tests 100}}))))

#?(:clj (defcheck spec-adder `adder))

(defn bad-adder [a b]
  (+ a b))

(s/fdef bad-adder
        :args (s/cat :a int? :b int?)
        :ret string?)

#?(:clj (defcheck ^::capture spec-bad-adder `bad-adder))

#?(:clj
   (deftest test-bad-adder
     (let [{:keys [test-out reports]} (capture-test-var #'spec-bad-adder)]
            (is (com.grzm.tespresso/lines-match?
                  [#"^FAIL in \(spec-bad-adder\)"
                   #"^val: -?\d+ fails at: \[:ret\] predicate: string\?$"
                   ""
                   #"^expected: string\?$"
                   #"^  actual: -?\d+$"]
                  test-out)))))

(defn test-ns-hook
  "Ignore tests with ::capture metadata."
  []
  (tespresso/test-ns-interns-sans-meta-key
    (ns-interns 'com.grzm.tespresso.spec-test) ::capture))
