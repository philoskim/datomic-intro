(ns datomic-guide.api.log
  (:require [datomic.api :as d]))

(use 'debux.core)


;;; db connection
(def db-uri "datomic:mem://api-log")

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


;; data install
(def user-data
  [{:db/id #db/id[:db.part/user -100]
    :user/id "wonderland"
    :user/name "alice"
    :user/e-mail "wonderland@gmail.com"}

   {:db/id #db/id[:db.part/user -101]
    :user/id "mississippi"
    :user/name "huckleberry"
    :user/e-mail "mississippi@gmail.com"}])
   
(def tx2 @(d/transact conn user-data))
tx2
; => {:db-before datomic.db.Db@37888178,
;     :db-after datomic.db.Db@aa62d884,
;     :tx-data [#datom[13194139534313 50 #inst "2017-11-07T05:28:02.689-00:00" 13194139534313 true]
;               #datom[17592186045418 63 "wonderland" 13194139534313 true]
;               #datom[17592186045418 64 "alice" 13194139534313 true]
;               #datom[17592186045418 65 "wonderland@gmail.com" 13194139534313 true]
;               #datom[17592186045419 63 "mississippi" 13194139534313 true]
;               #datom[17592186045419 64 "huckleberry" 13194139534313 true]
;               #datom[17592186045419 65 "mississippi@gmail.com" 13194139534313 true]],
;     :tempids {-9223350046622220388 17592186045418, -9223350046622220389 17592186045419}}

@(d/transact conn [[:db/add 17592186045418 :user/e-mail "wonderland@facebook.com"]])

;;; log 함수는 현재의 connection 값을 인수로 받아,
;;; transacrion 시간순으로 정렬된 새로운 connection 객체를 반환한다.
(def log (d/log conn))
log
; => #object[datomic.peer.LocalConnection$reify__8756 0x261fca19 "datomic.peer.LocalConnection$reify__8756@261fca19"]

(def tx-range (d/tx-range log nil nil))

tx-range
; => #object[datomic.log$tx_range$reify__8128 0x63e4bf4d "datomic.log$tx_range$reify__8128@63e4bf4d"]

(mapv (fn [tx] tx) tx-range)
; => [{:t 1000,
;      :data [#datom[13194139534312 50 #inst "2017-11-07T05:45:49.134-00:00" 13194139534312 true]
;             #datom[63 10 :user/id 13194139534312 true]
;             #datom[63 40 23 13194139534312 true]
;             #datom[63 42 37 13194139534312 true]
;             #datom[63 41 35 13194139534312 true]
;             #datom[64 10 :user/name 13194139534312 true]
;             #datom[64 40 23 13194139534312 true]
;             #datom[64 41 35 13194139534312 true]
;             #datom[65 10 :user/e-mail 13194139534312 true]
;             #datom[65 40 23 13194139534312 true]
;             #datom[65 42 38 13194139534312 true]
;             #datom[65 41 35 13194139534312 true]
;             #datom[0 13 65 13194139534312 true]
;             #datom[0 13 64 13194139534312 true]
;             #datom[0 13 63 13194139534312 true]]}
;     {:t 1001,
;      :data [#datom[13194139534313 50 #inst "2017-11-07T05:45:49.138-00:00" 13194139534313 true]
;             #datom[17592186045418 63 "wonderland" 13194139534313 true]
;             #datom[17592186045418 64 "alice" 13194139534313 true]
;             #datom[17592186045418 65 "wonderland@gmail.com" 13194139534313 true]
;             #datom[17592186045419 63 "mississippi" 13194139534313 true]
;             #datom[17592186045419 64 "huckleberry" 13194139534313 true]
;             #datom[17592186045419 65 "mississippi@gmail.com" 13194139534313 true]]}
;     {:t 1004, :data [#datom[13194139534316 50 #inst "2017-11-07T05:45:49.139-00:00" 13194139534316 true]
;                      #datom[17592186045418 65 "wonderland@facebook.com" 13194139534316 true]
;                      #datom[17592186045418 65 "wonderland@gmail.com" 13194139534316 false]]}]


;;; history 함수는 db 값을 인수로 받아, 모든 datom을 담은 새로운 db 값을 반환한다.
;;; 이 db 값은 질의(d/q 함수)에 사용할 수 있다.
(def hist-db (d/history (d/db conn)))

(d/q '[:find ?e ?e-mail ?tx ?added
       :in $hist
       :where [$hist ?e :user/id "wonderland"]
              [$hist ?e :user/e-mail ?e-mail ?tx ?added]]
     hist-db)
; => #{[17592186045418 "wonderland@facebook.com" 13194139534316 true]
;      [17592186045418 "wonderland@gmail.com" 13194139534313 true]
;      [17592186045418 "wonderland@gmail.com" 13194139534316 false]}

;; added?를 명시적으로 지정하지 않으면, true에 해당하는 datom만을 반환한다.
(d/q '[:find ?e ?e-mail
       :in $hist
       :where [$hist ?e :user/id "wonderland"]
              [$hist ?e :user/e-mail ?e-mail]]
     hist-db)
; => #{[17592186045418 "wonderland@facebook.com"]
;      [17592186045418 "wonderland@gmail.com"]}


;;; history 함수는 모든 datom을 반환한다. 즉, 어떤 datom도 필터링하지 않는 함수이다.
;;; 이에 반해, as-of 함수는 db와 시점 정보를 인수로 받아,  시점을 포함한 이전의 모든 datom을
;;; 반환하는 필터링 함수이고,
;;; since 함수는 db와 시점 정보를 인수로 받아, 주어진 시점을 제외한 이후의 모든 datom을 반환하는 필터링 함수이다.
;;; filter 함수는 db와 pred을 인수로 받아, pred을 통과하는 datom만을 반환하는 필터링 함수이다.


(d/basis-t (d/db conn))
; => 1004

(def db-1001 (d/as-of (d/db conn) 1001))
db-1001
; => datomic.db.Db@827e9c49

(d/as-of-t db-1001)
; => 1001


;; 이 함수의 인수로 주어진 db 값에 상관없이, 이 db의 연쇄를 통해 도달할 수 있는
;; 현재의 가장 높은 t값보다 더 큰 값을 반환한다. 그래서 1004가 아니라 1005를 반환한다. 
(d/next-t db-1001)
; => 1005



(def db-since (d/since (d/db conn) 1001))
db-since
; => datomic.db.Db@827e9c49

;; d/since 함수의 인수로 주어진 t값을 반환한다.
(d/since-t db-since)
; => 1001

(def t->tx (d/t->tx 1001))
t->tx
; => 13194139534313

(d/tx->t t->tx)
; => 1001



