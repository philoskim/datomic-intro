(ns datomic-guide.schema-example
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://schema-example")

(d/create-database db-uri)

(def conn (d/connect db-uri))


;; schema definition
(def schema-1
  [{:db/ident :user/id
    :db/valueType :db.type/string
    :db/unique :db.unique/value
    :db/cardinality :db.cardinality/one}

   {:db/ident :user/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :user/e-mail
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

@(d/transact conn schema-1)


;;; data install
(def user-data
  [{:db/id #db/id[:db.part/user]
    :user/id "alice"
    :user/name "Alice Parker"
    :user/e-mail "alice@gmail.com"}

   {:db/id #db/id[:db.part/user]
    :user/id "jack"
    :user/name "Jack Hinton"
    :user/e-mail "jack@gmail.com"}])
   
@(d/transact conn user-data)


(defn find-user [id]
  (d/q '[:find ?e .
         :in $ ?id
         :where [?e :user/id ?id]]
        (d/db conn) id))

(def alice-ent-id (find-user "alice"))
alice-ent-id ; => 17592186045418

(d/pull (d/db conn) '[*] alice-ent-id)
; => {:db/id 17592186045418,
;     :user/id "alice",
;     :user/name "Alice Parker",
;     :user/e-mail "alice@gmail.com"}


;;; field name :user/alias 추가
(def schema-2
  [{:db/ident :user/alias
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

@(d/transact conn schema-2)
@(d/transact conn [[:db/add alice :user/alias "wonderland"]])

(d/pull (d/db conn) '[*] alice-ent-id)
; => {:db/id 17592186045418,
;     :user/id "alice",
;     :user/name "Alice Parker",
;     :user/e-mail "alice@gmail.com",
;     :user/alias "wonderland"}


;;; field name :user/alias --> :user/nickname 으로 변경
(def alias-ent-id (d/entid (d/db conn) :user/alias))

@(d/transact conn [[:db/add alias-ent-id :db/ident :user/nickname]])
(d/pull (d/db conn) '[*] alice-end-id)
; => {:db/id 17592186045418,
;     :user/id "alice",
;     :user/name "Alice Parker",
;     :user/e-mail "alice@gmail.com",
;     :user/nickname "wonderland"}





  
  






