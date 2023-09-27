# pretty-sql-sbt-plugin

## Usage
1. Build plugin and push to local sbt repository using sbt-shell:
```sbt
publishLocal
```

2. Add plugin to your project by adding new line in file `project/plugins.sbt`:
```sbt
addSbtPlugin("pretty-sql-sbt-plugin" % "pretty-sql-sbt-plugin" % "0.1.0")
```

3. Run sbt-shell and execute task:
```sbt
formatSQL
```
All SQL code in Scala files should be formatted