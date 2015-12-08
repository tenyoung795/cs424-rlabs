default: Main.nxj

run: Main.nxj
	nxjupload -r $<

upload: Main.nxj
	nxjupload $<

Main.nxd: *.class
	nxjlink -od $@ Main

Main.nxj: *.class
	nxjlink -o $@ Main

Main.class: *.java
	nxjc Main.java

clean:
	rm -f Main.nxj Main.nxd *.class

.PHONY: default run upload clean
