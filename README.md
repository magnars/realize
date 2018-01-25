# realize

> real eyes realize real lies

Lazy sequences aren't data. They can throw exceptions. They can fire missiles.

This lib can't help you if you're firing missiles, but it will realize all lazy
sequences in a Clojure data structure, replacing them with
`{:realize.core/exception e}` if they throw an exception.

```clj
(require '[realize.core :as [realize]])

(def e (Exception. "Boom!"))

(def danger-seq (map (fn [_] (throw e)) [1 2 3]))

(realize/realize {:foo danger-seq})
;; => {:foo {:realize.core/exception e}}
```

It also has a tool to check if your data structure threw any exceptions when being realized:

```clj
(require '[realize.core :as realize])

(def e (Exception. "Boom!"))

(realize/find-exceptions {:foo {:realize.core/exception e}})
;; => [{:exception e :path [:foo]}]
```

## Install

Add `[realize "1.0.0"]` to `:dependencies` in your `project.clj`.

## Raison d'être

This lib was created so that Prone does not explode when given lazy-seqs that
throw exceptions when realized.

## Development

`lein test` will run all tests.

`lein test-refresh` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.

## License

Copyright © (iterate inc 2018) Magnar Sveen

[BSD-3-Clause](http://opensource.org/licenses/BSD-3-Clause), see LICENSE
