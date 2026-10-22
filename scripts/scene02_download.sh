#!/bin/bash
set -e
curl -fLsS --retry 2 --max-time 40 'https://media.canva.com/v2/image-resize/format:PNG/height:150/quality:100/uri:ifs%3A%2F%2FM%2F2018dc87-2384-4a53-a129-2aadc1117c4e/watermark:F/width:200?csig=AAAAAAAAAAAAAAAAAAAAAEKfhpgiOhy6RCDKrq1WytK1JcElPDDDHON1WhRyDhSv&exp=1792716273&osig=AAAAAAAAAAAAAAAAAAAAAIoWUQ5g44Sw9lwuuS4pOIDB8L1CslZb4yKIwv3L_PH_&signer=media-rpc&token=AAIAAU0AJDIwMThkYzg3LTIzODQtNGE1My1hMTI5LTJhYWRjMTExN2M0ZQAAAAABoWY3dWi7Fq-oFdtwDNRPfj4VHoIGqxExU234m-4eCU9XEZfI8g&x-canva-quality=thumbnail' -o app/src/main/res/drawable-nodpi/lesson11_new02.png
