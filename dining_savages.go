package main

import (
	"fmt"
	"sync"
)

var servingSize = 10
var savages = 100
var wg sync.WaitGroup

func main() {
	potEmpty := make(chan int, 1)
	pot := make(chan int, servingSize)
	potFull := make(chan int, 1)
	for i := 0; i < servingSize; i++ {
		pot <- 1
	}
	potFull <- 1
	for i := 0; i < savages; i++ {
		wg.Add(1)
		go eat(potEmpty, pot, potFull)
	}
	cook(potEmpty, pot, potFull)
	wg.Wait()
}

func cook(potEmpty, pot, potFull chan int) {
	for i := 0; i < (savages/servingSize)-1; i++ {
		//Wait till pot is empty
		<-potEmpty
		//Fill pot
		fmt.Println("Replenishing pot")
		for i := 0; i < servingSize; i++ {
			pot <- 1
		}
		potFull <- 1
	}
}

func eat(potEmpty, pot, potFull chan int) {
	defer wg.Done()
	for {
		<-potFull
		select {
		case <-pot:
			fmt.Printf("eating\n")
			potFull <- 1
			return
		default:
			potEmpty <- 1
		}
	}
}
