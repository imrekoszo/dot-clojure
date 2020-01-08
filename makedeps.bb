#!/usr/bin/env bb

(require '[clojure.edn :as edn]
         '[clojure.set :as set])

(def deps
  (-> *command-line-args*
      (first)
      (or "deps.edn")
      (slurp)
      (edn/read-string)))

(def secret
  (try
    (-> *command-line-args*
        (second)
        (or "secret-deps.edn")
        (slurp)
        (edn/read-string))
    (catch Exception _)))

(def all-deps (merge-with merge deps secret))

(def cursive-dev-aliases
  (juxt :bench :decompile :dev :hashp :kaocha :measure :nrebl :rebl-8
        :reflect :speculative :trace))

(-> all-deps
    (:aliases)
    (cursive-dev-aliases)
    (->> (reduce (fn [acc m] (merge-with merge acc m))))
    (select-keys [:extra-deps])
    (->> (assoc-in all-deps [:aliases :cursive-dev])))
