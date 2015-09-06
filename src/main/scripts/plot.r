data <- read.table("..\Desktop\stmt_r.scv", header=TRUE, sep=",")
boxplot(day ~ account, data=data)
boxplot(amount ~ account, data=data)
