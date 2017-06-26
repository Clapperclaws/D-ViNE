#!/bin/bash

for testcase in 'case.9.15.30' 'case.12.20.30' 'case.15.25.30' 'case.18.30.30' 'case.21.35.30'
do
  for num_shuffles in 25 50 75 100
  do
    python run_experiment.py --executable ./ml_vne --testcase_root $testcase --num_shuffles $num_shuffles
    cp -r $testcase $testcase.$num_shuffles
  done
done
