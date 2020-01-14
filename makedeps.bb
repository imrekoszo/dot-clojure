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

(def cursive-lite-aliases
  (-> #{:dev :hashp :nrebl :rebl-8}
      (sort)))

(def cursive-full-aliases
  (-> #{:bench :decompile :kaocha :measure
        :reflect :speculative :trace}
      (set/union cursive-lite-aliases)
      (sort)))

(->> all-deps
     (:aliases)
     ((apply juxt (map (partial apply juxt) [cursive-lite-aliases cursive-full-aliases])))
     (map (partial reduce (fn [acc m] (merge-with merge acc m))))
     (map #(select-keys % [:extra-deps]))
     (zipmap [:cursive-lite :cursive-full])
     (reduce (fn [deps alias] (update deps :aliases merge alias)) all-deps))
