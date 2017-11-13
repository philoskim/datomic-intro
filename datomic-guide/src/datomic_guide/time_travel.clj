(ns datomic-guide.time-travel
  (:require [datomic.api :as d]))

;;; db connection
(def db-uri "datomic:mem://time-travel")
(d/create-database db-uri)
(def conn (d/connect db-uri))


(d/db conn)   ; => datomic.db.Db@29475cf8


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

@(d/transact conn schema)
; => {:db-before datomic.db.Db@29475cf8,
;     :db-after datomic.db.Db@5ba75b5c,
;     :tx-data [#datom[13194139534312 50 #inst "2017-11-09T06:03:29.648-00:00" 13194139534312 true]
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
;     :tempids {-9223301668109598143 63, -9223301668109598142 64, -9223301668109598141 65}}

(d/db conn)   ; => datomic.db.Db@5ba75b5c


;; data install
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

(d/db conn)   ; => datomic.db.Db@d34f836c


(def alice-ent-id
  (d/q '[:find ?e .
         :where [?e :user/id "alice"]]
       (d/db conn)))

(d/pull (d/db conn) '[*] alice-ent-id)
; => {:db/id 17592186045418,
;     :user/id "alice",
;     :user/name "Alice Parker",
;     :user/e-mail "alice@gmail.com"}



@(d/transact conn [[:db/add alice-ent-id :user/e-mail "alice@facebook.com"]])

(d/pull (d/db conn) '[*] alice-ent-id)
; => {:db/id 17592186045418,
;     :user/id "alice",
;     :user/name "Alice Parker",
;     :user/e-mail "alice@facebook.com"}


(def hist-db (d/history (d/db conn)))

(d/q '[:find ?e ?e-mail ?tx
       :in $hist
       :where [$hist ?e :user/id "alice"]
              [$hist ?e :user/e-mail ?e-mail]]
     hist-db)
; => #{[17592186045418 "alice@gmail.com"]
;      [17592186045418 "alice@facebook.com"]}

