
JAVA=java
MIKADOTEST=mikadotest
MOBILITY=$(MIKADOTEST).mobility

check:
	$(JAVA) $(MOBILITY).NetMigratingCode && \
	$(JAVA) $(MOBILITY).FileMigratingCode && \
	$(JAVA) $(MOBILITY).FileMigratingCodeRecover && \
	for source in *.out; do \
	  echo -n "diff $$source $$source.good..."; \
	  if ! diff $$source $$source.good; \
	  then errors=1; else echo "OK"; fi; \
	done && test -z $$errors

store-backup:
	for source in *.out; do \
	  cp -f $$source $$source.good; \
	done