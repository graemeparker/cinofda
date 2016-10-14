#!/bin/bash

f=test.txt
for k in $(seq 1 100); do
  for i in $(seq 1 10); do 
    echo $i >> $f
    sleep 1
  done 
  rm $f
  sleep 1
done
