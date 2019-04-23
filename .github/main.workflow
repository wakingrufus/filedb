workflow "New workflow" {
  on = "push"
  resolves = ["Gradle test"]
}

action "Gradle test" {
  uses = "MrRamych/gradle-actions/openjdk-8@2.1"
  args = "check"
}
