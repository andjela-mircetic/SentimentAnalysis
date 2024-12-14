(ns benchmark
  (:require [criterium.core :refer [bench quick-bench]]
            [chatsanalyzer :refer [calculate-chat-rate
                                   calculate-chat-rate-using-loop]]))

(def mock-chat-messages [{:messageRate 3} {:messageRate 4} {:messageRate 2}])

; Benchmarking the `calculate-chat-rate` function using `reduce` and `map`
(bench (calculate-chat-rate mock-chat-messages))
; Evaluation count : 184413000 in 60 samples of 3073550 calls.
;              Execution time mean : 344.194287 ns
;     Execution time std-deviation : 2.910902 ns
;    Execution time lower quantile : 338.795498 ns ( 2.5%)
;    Execution time upper quantile : 349.491580 ns (97.5%)
;                    Overhead used : 5.954750 ns
; 
; Found 2 outliers in 60 samples (3.3333 %)
; 	low-severe	 2 (3.3333 %)
;  Variance from outliers : 1.6389 % Variance is slightly inflated by outliers

;; Relatively stable performance, though we do observe some minor variance due to outliers.



; Benchmarking the `calculate-chat-rate-using-loop` function with `loop/recur`
(bench (calculate-chat-rate-using-loop mock-chat-messages))
; Evaluation count : 184673580 in 60 samples of 3077893 calls.
;              Execution time mean : 339.119126 ns
;     Execution time std-deviation : 5.550067 ns
;    Execution time lower quantile : 337.117267 ns ( 2.5%)
;    Execution time upper quantile : 351.903407 ns (97.5%)
;                    Overhead used : 5.954750 ns
; 
; Found 4 outliers in 60 samples (6.6667 %)
; 	low-severe	 1 (1.6667 %)
; 	low-mild	 3 (5.0000 %)
;  Variance from outliers : 4.3224 % Variance is slightly inflated by outliers

;; The presence of more outliers indicates less stability in performance compared to the `reduce`/`map` approach.



; Conclusion:
; Based on the benchmarking results, both implementations of `calculate-chat-rate` (using `reduce`/`map` and `loop/recur`) perform very similarly,
; with mean execution times of 344.19 ns and 339.12 ns, respectively. However, the `reduce`/`map` version demonstrates slightly more stability 
; in execution time, with fewer outliers and a smaller standard deviation.
