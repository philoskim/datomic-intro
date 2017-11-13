(ns datomic-guide.api.basic
  (:require [datomic.api :as d]))

(use 'debux.core)


;;; db connection
(def db-uri "datomic:mem://api-basic")

(d/create-database db-uri)

(def conn (d/connect db-uri))


;; schema install
(def schema
  [{:db/ident :user/id
    :db/valueType :db.type/string
    :db/unique :db.unique/value
    :db/cardinality :db.cardinality/one}

   {:db/ident :user/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :user/e-mail
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}])

(def tx1 @(d/transact conn schema))

tx1
; => {:db-before datomic.db.Db@31cdbf64,
;     :db-after datomic.db.Db@c8dd9f68,
;     :tx-data [#datom[13194139534312 50 #inst "2017-11-07T10:45:54.700-00:00" 13194139534312 true]
;               #datom[63 10 :user/id 13194139534312 true]
;               #datom[63 40 23 13194139534312 true]
;               #datom[63 42 37 13194139534312 true]
;               #datom[63 41 35 13194139534312 true]
;               #datom[64 10 :user/name 13194139534312 true]
;               #datom[64 40 23 13194139534312 true]
;               #datom[64 41 35 13194139534312 true]
;               #datom[65 10 :user/e-mail 13194139534312 true]
;               #datom[65 40 23 13194139534312 true]
;               #datom[65 42 38 13194139534312 true]
;               #datom[65 41 35 13194139534312 true]
;               #datom[0 13 65 13194139534312 true]
;               #datom[0 13 64 13194139534312 true]
;               #datom[0 13 63 13194139534312 true]],
;     :tempids {-9223301668109598140 63, -9223301668109598139 64, -9223301668109598138 65}}

(d/attribute (d/db conn) :user/e-mail)
; => #AttrInfo{:id 65
;              :ident :user/e-mail
;              :value-type :db.type/string
;              :cardinality :db.cardinality/one
;              :indexed false
;              :has-avet true
;              :unique :db.unique/identity
;              :is-component false
;              :no-history false
;              :fulltext false}

(def ent-id (d/entid (d/db conn) :user/e-mail))
; => 65

(d/ident (d/db conn) ent-id)
; => :user/e-mail


(def entity (d/entity (d/db conn) ent-id))
; => {:db/id 65}

(d/touch entity)
; => {:db/id 65,
;     :db/ident :user/e-mail,
;     :db/valueType :db.type/string,
;     :db/cardinality :db.cardinality/one,
;     :db/unique :db.unique/identity}

(d/entity-db entity)
; => datomic.db.Db@c8dd9f68

(d/pull (d/db conn) '[*] ent-id)
; => {:db/id 65,
;     :db/ident :user/e-mail,
;     :db/valueType {:db/id 23},
;     :db/cardinality {:db/id 35},
;     :db/unique {:db/id 38}}


