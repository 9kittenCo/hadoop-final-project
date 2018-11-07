name := "hive_udf_ip"

version := "0.1"

scalaVersion := "2.11.12"

resolvers += "conjars" at "http://conjars.org/repo"

//libraryDependencies += "org.apache.hadoop" %  "hadoop-common"      % "2.6.0"     % "provided"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.apache.hadoop" %  "hadoop-core"        % "1.2.1"    % "provided",
  "org.apache.hive"   %  "hive-exec"          % "1.1.0"    % "provided",
  "commons-net"       % "commons-net"         % "3.6"      % "provided"
)

//libraryDependencies ++= Seq(
//  "commons-net"       % "commons-net"   % "3.6"    % "provided",
//  "org.apache.hive"   % "hive-exec"     % "1.1.0"  % "provided",
//  "org.apache.hadoop" % "hadoop-core"   % "0.20.2" % "provided"
//)

mainClass in assembly := Some("com.nykytenko.IpToGeo")

assemblyMergeStrategy in assembly := {
  case PathList("org","aopalliance", xs @ _*)       => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*)        => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*)         => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*)     => MergeStrategy.last
  case PathList("org", "apache", xs @ _*)           => MergeStrategy.last
  case PathList("com", "google", xs @ _*)           => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*)         => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*)           => MergeStrategy.last
  case "about.html"                 => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA"      => MergeStrategy.last
  case "META-INF/mailcap"           => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties"          => MergeStrategy.last
  case "git.properties"             => MergeStrategy.last
  case "log4j.properties"           => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := s"${name.value}-${scalaVersion.value}-${version.value}.jar"