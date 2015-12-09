default: Main.nxj

run: Main.nxj
	nxjupload -r $<

upload: Main.nxj
	nxjupload $<

Main.nxj: Main.class
	nxjlink -o $@ Main

Main.class: Main.java Track.java Node.java Direction.java Turn.java Orientation.java
	nxjc Main.java

clean:
	rm -f Main.nxj Main.nxd *.class

.PHONY: default run upload clean
