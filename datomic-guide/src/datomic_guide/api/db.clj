(ns datomic-guide.api.db
  (:require [datomic.api :as d]))

(use 'debux.core)


(def db-uri "datomic:mem://api-db")

(d/create-database db-uri)
; => true

(d/get-database-names "datomic:mem://*")
; => ("api-db")


(d/rename-database db-uri "api-db2")
; => true

(d/get-database-names "datomic:mem://*")
; => ("api-db2")


(d/delete-database "datomic:mem://api-db2")
; => true

(d/get-database-names "datomic:mem://*")
; => nil

