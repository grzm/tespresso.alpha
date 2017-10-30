# tespresso

Test with a little caffeine.

Tespresso is a collection of `clojure.test` helpers.

## Usage

### Test `clojure.spec`

Tespresso provides a `defspec` macro for testing as well as a `check?`
assert-expr to exercise individual specs.

```clojure
(require
  '[clojure.test :refer [is]]
  '[com.grzm.tespresso :as tespresso])

(tespresso/defspec ,,,)

(is (com.grzm.tespresso.spec-test/check? ,,,))
```

### Test `ex-data`

Clojure's ex-info is great. Confirm you're getting back what you
expect when an exception is thrown.

```clojure
(is (com.grzm.tespresso/thrown-with-data? ,,,))

(tespresso/ex-data-keys= ,,,)
```


### Test Component systems

The `with-system` and `with-system-options` macros help testing
systems built with Stuart Sierra's component library.

```clojure
(tespresso/with-system ,,,)

(tespresso/with-system-options ,,,)
```

### Test logging
```clojure
(require '[com.grzm.tespresso.clojure-tools-logging :as ctl])
(ctl/with-logging ,,,)
```

### Compare bytes

```clojure
(is (com.grzm.tespresso/bytes= ,,,))
```

### But,… tespresso has no tests!

Not yet. Ironic, isn't it? They'll get there, eventually.

## License

© 2017 Michael Glaesemann

Released under the MIT License. See LICENSE for details.
