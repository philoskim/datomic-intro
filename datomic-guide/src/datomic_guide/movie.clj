(ns datomic-guide.movie
  (:require [clojure.java.io :as io]
            [datomic.api :as d]))

(use 'debux.core)

;; db creation
(def db-uri "datomic:mem://movies")

(d/create-database db-uri)


;; db connection
(def conn (d/connect db-uri))


;; schema install
(def movie-schema
  (-> (io/resource "movie-schema.edn")
      slurp
      read-string))

@(d/transact conn movie-schema)


;; data install
(def movie-data
  (-> (io/resource "movie-data.edn")
      slurp
      read-string))

@(d/transact conn movie-data)


;;;
;;; query examples
;;;

(d/q '[:find ?title
       :where [?e :movie/year 1987]
              [?e :movie/title ?title]]
     (d/db conn))
; => #{["RoboCop"] ["Lethal Weapon"] ["Predator"]}


(d/q '[:find ?title
       :in $ ?year
       :where [?e :movie/year ?year]
              [?e :movie/title ?title]]
     (d/db conn) 1987)
; => #{["RoboCop"] ["Lethal Weapon"] ["Predator"]}


(d/q '[:find ?title ?year
       :where [?m :movie/title ?title]
              [?m :movie/year ?year]
              [(< ?year 1984)]]
     (d/db conn))
; => #{["Alien" 1979] ["Mad Max" 1979] ["First Blood" 1982] ["Mad Max 2" 1981]}




