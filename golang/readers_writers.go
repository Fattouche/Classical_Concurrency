package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"

	"github.com/pkg/profile"
)

var myGlobal int
var mutex sync.RWMutex
var wg sync.WaitGroup

func sleepThread() {
	time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
}

func main() {
	defer profile.Start().Stop()
	totalReaders := 10
	totalWriters := 5
	wg.Add(totalReaders)
	for i := 0; i < totalReaders; i++ {
		go read()
	}
	wg.Add(totalWriters)
	for i := 0; i < totalWriters; i++ {
		go write()
	}
	wg.Wait()
}

func read() {
	sleepThread()
	mutex.RLock()
	fmt.Println("Read: ", myGlobal)
	sleepThread()
	mutex.RUnlock()
	wg.Done()
}

func write() {
	sleepThread()
	mutex.Lock()
	temp := myGlobal
	myGlobal++
	fmt.Printf("Write changed from %d to %d\n", temp, myGlobal)
	sleepThread()
	mutex.Unlock()
	wg.Done()
}
