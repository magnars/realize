(ns realize.core-test
  (:require [clojure.data.generators :as gen]
            [clojure.test.generative :as test :refer [defspec]]
            [clojure.test.generative.runner :as runner]
            [clojure.test :refer [deftest testing is]]
            [datomic.api :as d]
            [realize.core :as sut]))

(defn is-lazy? [form]
  (instance? clojure.lang.LazySeq form))

(defn collection-including-lazy-seqs []
  (let [[coll args] (gen/rand-nth (conj gen/collections
                                    [gen/list [gen/scalars]]))]
    (apply coll (map gen/rand-nth args))))

(defspec all-seqs-are-realized-but-the-same
  sut/realize
  [^{:tag (realize.core-test/collection-including-lazy-seqs)} form]
  (assert (= form %))
  (clojure.walk/prewalk (fn [f]
                          (when (and (is-lazy? f)
                                     (not (realized? f)))
                            (throw (AssertionError. "lazy but not realized!")))
                          f)
                        %))

(def tests (runner/get-tests #'all-seqs-are-realized-but-the-same))

(def e (Exception. "Boom!"))
(def e2 (Exception. "Bang!"))
(def datomic-entity (d/entity (d/db (do (d/create-database "datomic:mem://test-db")
                                        (d/connect "datomic:mem://test-db"))) 1))

(deftest realize
  (testing "no errors"
    (is (= '(1 2 3) (sut/realize (map identity [1 2 3]))))
    (is (= 0 (:failures (runner/run-suite {:nthreads 2 :msec 1000} tests)))))

  (testing "at root"
    (is (= {:realize.core/exception e}
           (sut/realize (map (fn [_] (throw e)) [1 2 3])))))

  (testing "nested in map"
    (is (= {:foo {:realize.core/exception e}}
           (sut/realize {:foo (map (fn [_] (throw e)) [1 2 3])}))))

  (testing "nested in vec"
    (is (= [:before {:realize.core/exception e} :after]
           (sut/realize [:before (map (fn [_] (throw e)) [1 2 3]) :after]))))

  (testing "deeply nested"
    (is (= {:foo {:bar [:baz {:boo {:realize.core/exception e}}]}}
           (sut/realize {:foo {:bar [:baz {:boo (map (fn [_] (throw e)) [1 2 3])}]}}))))

  (testing "don't walk into collections that cannot be reconstructed via empty"
    (is (= datomic-entity (sut/realize datomic-entity)))))

(deftest find-exceptions
  (testing "no errors"
    (is (empty? (sut/find-exceptions '(1 2 3)))))

  (testing "at root"
    (is (= [{:exception e :path []}]
           (sut/find-exceptions {:realize.core/exception e}))))

  (testing "nested in map value"
    (is (= [{:exception e :path [:foo]}]
           (sut/find-exceptions {:foo {:realize.core/exception e}}))))

  (testing "nested in map key"
    (is (= [{:exception e :path []}]
           (sut/find-exceptions {{:realize.core/exception e} :bar}))))

  (testing "nested in vec"
    (is (= [{:exception e :path [1]}]
           (sut/find-exceptions [:before {:realize.core/exception e} :after]))))

  (testing "deeply nested"
    (is (= [{:exception e :path [:foo :bar 1 :boo]}]
           (sut/find-exceptions {:foo {:bar [:baz {:boo {:realize.core/exception e}}]}}))))

  (testing "multiple exceptions"
    (is (= [{:exception e :path [:foo :bar 1 :boo]}
            {:exception e2 :path [:foo :far 4]}]
           (sut/find-exceptions {:foo {:bar [:baz {:boo {:realize.core/exception e}}]
                                       :far (list 0 1 2 3 {:realize.core/exception e2})}})))))
