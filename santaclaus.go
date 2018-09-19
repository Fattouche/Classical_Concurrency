package main

import (
	"fmt"
	"math/rand"
	"os"
	"sync"
	"time"
)

var reindeer = 9
var elves = 100
var wg sync.WaitGroup

func main() {
	//Buffered to number of reindeers remaining
	reindeersRemaining := make(chan int, reindeer)
	initReindeer(reindeersRemaining)

	reindeersFinished := make(chan int)
	santaAvailable := make(chan int)
	threeElves := make(chan int)
	elvesWaiting := make(chan int, 3)

	//Wait for reindeer to finish
	go waitForReindeer(reindeersFinished)
	//Start santa helping elves
	go helpElves(santaAvailable, threeElves)

	wg.Add(elves)
	//Start the elves at random times
	go func() {
		for i := 0; i < elves; i++ {
			time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
			go getHelp(santaAvailable, elvesWaiting, threeElves, i)
		}
	}()

	go func() {
		for i := 0; i < reindeer; i++ {
			time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond)
			go arriveReindeer(reindeersRemaining, reindeersFinished)
		}
	}()
	//Make sure we wait till all elves have finished or the last reindeer arrives
	wg.Wait()
}

func initReindeer(reindeersRemaining chan int) {
	//Initialize to 9 reindeers remaining
	for i := 0; i < 9; i++ {
		reindeersRemaining <- 1
	}
}

func arriveReindeer(reindeersRemaining, reindeersFinished chan int) {
	<-reindeersRemaining
	fmt.Println("New reindeer arrived")
	if len(reindeersRemaining) == 0 {
		fmt.Println("Last reindeer arrived, notifying santa")
		reindeersFinished <- 1
	}
}

func waitForReindeer(reindeersFinished chan int) {
	//Unbufferred channel, blocks till reindeers finished
	<-reindeersFinished
	fmt.Println("Reindeers finished, hitching sled!")
	os.Exit(0)
}

func helpElves(santaAvailable, threeElves chan int) {
	for {
		//Wait till three elves have arrived
		<-threeElves
		fmt.Println("Helping three elves")
		//Signal you are available again
		santaAvailable <- 1
		//Helping
		time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
	}
}

func getHelp(santaAvailable, elvesWaiting, threeElves chan int, id int) {
	fmt.Printf("elf: %d waiting for santas help\n", id)
	//Go into the queue of elves waiting
	elvesWaiting <- 1
	//If you are third elf, notify santa
	if len(elvesWaiting) == 3 {
		for i := 0; i < 3; i++ {
			//Clear all elves waiting before signaling that three elves are groupe
			<-elvesWaiting
		}
		threeElves <- 1
	}
	<-santaAvailable
	//Get santas help
	time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
	//No longer waiting
	wg.Done()
}
