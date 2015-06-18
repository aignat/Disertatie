compile:
	mkdir -p classes
	javac -sourcepath src -d classes -classpath resources/jwnl14-rc2/lib/*:. src/main/*.java 

run_synonyms: compile
	java -cp classes:resources/jwnl14-rc2/lib/* main.Main

run_peaks: compile
	java -cp classes:resources/jwnl14-rc2/lib/* main.PeaksFinder

run_intersections: compile
	java -cp classes:resources/jwnl14-rc2/lib/* main.IntersectionFinder

clean:
	rm -rf classes
