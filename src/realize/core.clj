(ns realize.core)

(defn realize [form]
  (try
    (cond
      (list? form) (apply list (map realize form))
      (instance? clojure.lang.IMapEntry form) (vec (map realize form))
      (seq? form) (doall (map realize form))
      (instance? clojure.lang.IRecord form) (reduce (fn [r x] (conj r (realize x))) form form)
      (coll? form) (into (empty form) (map realize form))
      :else form)
    (catch Throwable e {::exception e})))

(defn find-exceptions
  ([form] (find-exceptions form []))
  ([form path]
   (cond
     (and (map? form) (::exception form)) [{:exception (::exception form) :path path}]
     (map? form) (mapcat (fn [[k v]]
                           (concat
                            (find-exceptions k path)
                            (find-exceptions v (conj path k))))
                         form)
     (coll? form) (mapcat (fn [i v] (find-exceptions v (conj path i)))
                          (range)
                          form))))
