(ns datomic-guide.api.tempid
  (:require [datomic.api :as d]))

(use 'debux.core)

; tag literal 방식이나 tempid 함수 호출 방식 모두 같은 일을 한다. 다만, tag literal 방식은
; literal source 입력 시에 많이 사용하고, tempid 함수 호출 방식은 프로그래밍을 통해 임시 id를
; 동적으로 만들어야 할 때 주로 사용한다.

;; tag literal 방식으로 tempid 생성
#db/id[:db.part/user]
; => #db/id[:db.part/user -1000004]

#db/id[:db.part/user -100]
; => #db/id[:db.part/user -100]


;; tempid 함수 호출 방식으로 tempid 생성
(d/tempid :db/part/user)
; => #db/id[:db/part/user -1000005]

(d/tempid :db/part/user -100)
; => #db/id[:db/part/user -100]


;;; db connection
(def db-uri "datomic:mem://api-tempid")

(d/create-database db-uri)
; => true

(def conn (d/connect db-uri))
conn
; => #object[datomic.peer.LocalConnection 0x11ea2217 "datomic.peer.LocalConnection@11ea2217"]


;; schema install
(def schema
  [{:db/id (d/tempid :db.part/db -1000)
    :db/ident :user/id
    :db/valueType :db.type/string
    :db/unique :db.unique/value
    :db/cardinality :db.cardinality/one}

   {:db/id (d/tempid :db.part/db -2000)
    :db/ident :user/e-mail
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}])

(def tx @(d/transact conn schema))
(dbg tx)
; => {:db-before datomic.db.Db@8edbccf1,
;     :db-after datomic.db.Db@a349dc06,
;     :tx-data [#datom[13194139534312 50 #inst "2017-11-07T04:55:10.246-00:00" 13194139534312 true]
;               #datom[63 10 :user/id 13194139534312 true]
;               #datom[63 40 23 13194139534312 true]
;               #datom[63 42 37 13194139534312 true]
;               #datom[63 41 35 13194139534312 true]
;               #datom[64 10 :user/e-mail 13194139534312 true]
;               #datom[64 40 23 13194139534312 true]
;               #datom[64 42 38 13194139534312 true]
;               #datom[64 41 35 13194139534312 true]
;               #datom[0 13 64 13194139534312 true]
;               #datom[0 13 63 13194139534312 true]],
;     :tempids {-9223301668109598143 63, -9223301668109598142 64}}


(d/db conn)
; => datomic.db.Db@a349dc06


;; resolve-tempid는 transact할 때 이미 사용한 tempid를 대상으로 actual id를 반환한다. 
(d/resolve-tempid (d/db conn) (:tempids tx) -9223301668109598143)
; => 63


