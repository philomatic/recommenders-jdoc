Example Usage:

./livedoc org.sonatype.aether:aether-util:1.13.1 -r http://repo1.maven.org/maven2/

This will download and javadoc the specified maven artifact, but don't save the result to a folder on your hard drive (a 'dry-run' if you want so).
For an output to the current directory (where javadoc executable is located) type:

./livedoc org.sonatype.aether:aether-util:1.13.1 -r http://repo1.maven.org/maven2/ -d .