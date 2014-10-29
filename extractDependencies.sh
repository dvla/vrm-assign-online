sbt dependencies | grep \| | grep -v + | cut -f 2 -d\| | sort -u > webdependencies
