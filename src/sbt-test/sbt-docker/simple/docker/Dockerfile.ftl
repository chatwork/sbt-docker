FROM busybox
ADD bin/echo.sh /
CMD ["sh", "/echo.sh", "${name}-${version}"]