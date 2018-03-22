# tespresso

Test with a little caffeine.

Tespresso is a collection of `clojure.test` helpers.

## Releases and Dependency Information

Releases are on [Clojars](https://clojars.org/com.grzm/tespresso.alpha).

### Clojure [CLI/deps.edn][deps] coordinates:

```clojure
{com.grzm/tespresso.alpha {:mvn/version "0.1.8"}}
```

### [Leiningen][]/[Boot][] dependency information:

```clojure
[com.grzm/tespresso.alpha "0.1.8"]
```

### [Maven] dependency information:

```xml
<dependency>
  <groupId>com.grzm</groupId>
  <artifactId>tespresso.alpha</artifactId>
  <version>0.1.8</version>
</dependency>
```

[deps]: https://clojure.org/reference/deps_and_cli
[Leiningen]: http://leiningen.org/
[Boot]: http://boot-clj.com
[Maven]: http://maven.apache.org/


## Usage

### Clojure Spec helpers

Tespresso provides a `defcheck` macro for testing as well as a `check?`
assert-expr to exercise individual specs.

```clojure
(ns com.example.spec-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.spec.alpha :refer [defcheck]))

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
```

The `defcheck` macro provides a convenient shorthand for this simple case.

```clojure
(defcheck spec-test-adder `adder)
```

`defcheck` is currently Clojure only.

### Test ex-data

Clojure's ex-info is great. Confirm you're getting back what you
expect when an exception is thrown.

```clojure
(ns com.example.thrown-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.alpha :as tespresso]))

(deftest has-expected-ex-data
  (is (com.grzm.tespresso/thrown-with-data?
        #(= ::self-inflicted (:cause %))
        (throw (ex-info "yeah, I'm gonna throw"
                        {:cause ::self-inflicted,
                         :a-key :a-value
                         :another-key :another-value})))))
```

Helper functions `tespresso/ex-data=` and `tespresso/ex-data-select=`
provide convenient short-hands for matching all or partial key-values
in `ex-data`.

```clojure
;; match all keys and values
(deftest has-expected-ex-data-full-match
  (is (com.grzm.tespresso/thrown-with-data?
        (tespresso/ex-data= {:cause ::self-inflicted,
                             :a-key :a-value,
                             :another-key :another-value})
        (throw (ex-info "Still throwin'"
                        {:cause ::self-inflicted,
                         :a-key :a-value
                         :another-key :another-value})))))

;; match just the key-vals provided
(deftest has-expected-ex-data-select-match
  (is (com.grzm.tespresso/thrown-with-data?
        (tespresso/ex-data-select= {:cause ::self-inflicted,
                                    :a-key :a-value})
        (throw (ex-info "Can't stop me now!"
                        {:cause ::self-inflicted,
                         :a-key :a-value
                         :another-key :another-value})))))
```

An optional regex can be passed to also match the message.

```clojure
;; match just the key-vals provided
(deftest has-expected-ex-data-select-match
  (is (com.grzm.tespresso/thrown-with-data?
        #"^Can't stop"
        (tespresso/ex-data-select= {:cause ::self-inflicted,
                                    :a-key :a-value})
        (throw (ex-info "Can't stop me now!"
                        {:cause ::self-inflicted,
                         :a-key :a-value
                         :another-key :another-value})))))
```

### Test Component systems

The `with-system` and `with-system-options` macros help testing
systems built with [Stuart Sierra's component library.][component-lib]

[component-lib]: https://github.com/stuartsierra/component

The `with-system` macro takes an initial value of a system, starts it,
and makes it available as the value of an atom. After evaluating the body,
the system is stopped.

```clojure
(require
  '[com.grzm.tespresso.component.alpha :refer [with-system])

(with-system [sys init-sys-value]
   ;; access system value by deferencing `sys` atom
   (is (= [:app :db] (keys @sys))))
```

### Test clojure.tools.logging

Sometimes you might want to confirm logging is working as expected.
Test `clojure.tools.logging` using the `with-logging` macro to capture
log output. Available for Clojure only.

```clojure
(ns com.example.tools-logging-test
  (:require
   [clojure.test :refer [deftest is]]
   [clojure.tools.logging :as log]
   [com.grzm.tespresso.tools-logging.alpha :refer [with-logging]]))

(deftest test-logging
  (with-logging [#{:info :warn} log-entries]
    (log/tracef "Some trace message")
    (log/infof "Some info message")
    (log/warnf "Some example warning")
    (is (= [["com.example.tools-logging-test" :info nil "Some info message"]
            ["com.example.tools-logging-test" :warn nil "Some example warning"]]
           @log-entries))))
```

### Compare bytes

In Clojure, com.grzm.tespresso.bytes relies on Zach Tellman's
[byte-streams library.][byte-streams]

[byte-streams]: https://github.com/ztellman/byte-streams

```clojure
(ns com.example.bytes-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.alpha :as tespresso :refer [capture-test-var]]
   [com.grzm.tespresso.bytes.alpha :as bytes]))

(deftest byte-inequality-example
  (is (com.grzm.tespresso.bytes/bytes=
        (bytes/byte-buffer [0 1 (int \a) 3])
        (bytes/byte-buffer [0 2 4 3]))))
```

### Test clojure.test?

When developing new assertions and test macros, it's helpful to
actually test the test output, particularly when you want to confirm a
failing test reports correctly. Relying on visual inspection of test
run output is problematic, not only because you might miss details,
but also because a test that's failing *unexpectedly* may get lost in
the *expected* failing output.

```
(ns com.example.test-test-output
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.tespresso.alpha :as tespresso :refer [capture-test-var]))

(deftest ^::capture test-to-capture
  (is (= 1 2)))

(deftest test-capture
  (let [{:keys [test-out] :as capture} (capture-test-var #'test-to-capture)]
    ;; com.grzm.tespresso/lines-match? takes an sequence of
    ;; regex patterns or strings to match against the output
    (is (com.grzm.tespresso/lines-match?
          [#"FAIL in \(test-to-capture\)"
           "expected: (= 1 2)"
           "  actual: (not (= 1 2))"]
           test-out))))

;; implement test-ns-hook to ignore marked tests during a test run
(defn test-ns-hook
  []
  (tespresso/test-ns-interns-sans-meta-key
    (ns-interns 'com.example.test-test-output) ::capture))
```

`capture-test-var` returns a map of `:test-out`, `:out`,
`:report-counters`, and `:reports`.

Note that while `test-ns-hook` is supported by clojure.test (and cljs.test),
not all test runners support `test-ns-hook`. The `test-ns-interns-sans-meta-key`



## Tespresso eats its own dog food

For more examples of Tespresso in action, take a look at [Tespresso's
own tests.](test/src/com/grzm/tespresso)


## Testing Tespresso

Testing Clojure implementation

    lein test

Testing JVM ClojureScript implementation

    lein clean && lein cljsbuild once

Testing self-hosted ClojureScript implementation

    scripts/test-self-host

Not all functionality is tested under self-hosted environments.

## License

© 2017-2018 Michael Glaesemann

Released under the MIT License. See LICENSE for details.
