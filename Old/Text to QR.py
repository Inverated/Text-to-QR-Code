import qrcode
from PIL import Image

def url_to_qr(): 
    qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
    #qr.add_data("0022024051400025986")
    qr.add_data("https://www.canva.com/design/DAF1tdMBEzQ/9dpQZePU5Jso5_Ln24zr_g/view")
    #qr.add_data("https://form.gov.sg/656d1d998f103d00120026c3")
    
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")

    img.save("Output.png")
    return

    #logo = Image.open("705 SQN Logo.png").convert("RGBA")
    qr_size = img.size[0]
    new_length = int(qr_size / 2)
    
    #logo_width, logo_height = logo.size
    #new_width = round( logo_width / (logo_height/new_length) )

    position = ((qr_size - new_length) // 2, (qr_size - new_length) // 2)

    #logo = logo.resize((new_width, new_length), Image.ANTIALIAS)
    #img.paste(logo, position, logo)

    img.save("Output.png")
    print("Done")
    
    
url_to_qr()
