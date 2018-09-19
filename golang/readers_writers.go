package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var myGlobal int
var mutex sync.RWMutex
var wg sync.WaitGroup

func sleep() {
	time.Sleep(time.Duration(rand.Intn(500)) * time.Millisecond)
}

func main() {
	fmt.Println("Starting readers")
	wg.Add(10)
	for i := 0; i < 10; i++ {
		go read()
	}
	fmt.Println("Start writers")
	wg.Add(5)
	for i := 0; i < 5; i++ {
		go write()
	}
	wg.Wait()
}

func read() {
	sleep()
	mutex.RLock()
	fmt.Println("Read: ", myGlobal)
	sleep()
	mutex.RUnlock()
	wg.Done()
}

func write() {
	sleep()
	mutex.Lock()
	temp := myGlobal
	myGlobal++
	fmt.Printf("Write changed from %d to %d\n", temp, myGlobal)
	sleep()
	mutex.Unlock()
	wg.Done()
}
