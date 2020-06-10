package sdk.chat.core.avatar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sdk.chat.core.dao.User;

// This needs SVG so currently doesn't work :/
public class AvataaarsGenerator implements AvatarGenerator {

    public enum AvatarStyle {
        Transparent,
        Circle
    }

    public enum TopType {
        NoHair,
        Eyepatch,
        Hat,
        Hijab,
        Turban,
        WinterHat1,
        WinterHat2,
        WinterHat3,
        WinterHat4,
        LongHairBigHair,
        LongHairBob,
        LongHairBun,
        LongHairCurly,
        LongHairCurvy,
        LongHairDreads,
        LongHairFrida,
        LongHairFro,
        LongHairFroBand,
        LongHairNotTooLong,
        LongHairShavedSides,
        LongHairMiaWallace,
        LongHairStraight,
        LongHairStraight2,
        LongHairStraightStrand,
        ShortHairDreads01,
        ShortHairDreads02
    }

    public enum AccessoriesType {
        Blank,
        Kurt,
        Prescription01,
        Prescription02,
        Round,
        Sunglasses,
        Wayfarers
    }

    public enum HatColor {
        Black,
        Blue01,
        Blue02,
        Blue03,
        Gray01,
        Gray02,
        Heather,
        PastelBlue,
        PastelGreen,
        PastelOrange,
        PastelRed,
        PastelYellow,
        Pink,
        Red,
        White
    }

    public enum HairColor {
        Auburn,
        Black,
        Blonde,
        BlondeGolden,
        Brown,
        BrownDark,
        PastelPink,
        Platinum,
        Red,
        SilverGray
    }

    public enum FacialHairType {
        Blank,
        BeardMedium,
        BeardLight,
        BeardMajestic,
        MoustacheFancy,
        MoustacheMagnum
    }
    public enum FacialHairColor {
        Auburn,
        Black,
        Blonde,
        BlondeGolden,
        Brown,
        BrownDark,
        Platinum,
        Red
    }

    public enum ClotheType {
        BlazerShirt,
        BlazerSweater,
        CollarSweater,
        GraphicShirt,
        Hoodie,
        Overall,
        ShirtCrewNeck,
        ShirtScoopNeck,
        ShirtVNeck
    }
    public enum ClotheColor {
        Black,
        Blue01,
        Blue02,
        Blue03,
        Gray01,
        Gray02,
        Heather,
        PastelBlue,
        PastelGreen,
        PastelOrange,
        PastelRed,
        PastelYellow,
        Pink,
        Red,
        White
    }

    public enum GraphicType {
        Bat,
        Cumbia,
        Deer,
        Diamond,
        Hola,
        Pizza,
        Resist,
        Selena,
        Bear,
        SkullOutline,
        Skull
    }

    public enum EyeType {
       Close,
       Cry,
       Default,
       Dizzy,
       EyeRoll,
       Happy,
       Hearts,
       Side,
       Squint,
       Surprised,
       Wink,
       WinkWacky
    }

    public enum EyebrowType {
        Angry,
        AngryNatural,
        Default,
        DefaultNatural,
        FlatNatural,
        RaisedExcited,
        RaisedExcitedNatural,
        SadConcerned,
        SadConcernedNatural,
        UnibrowNatural,
        UpDown,
        UpDownNatural
    }

    public enum MouthType {
        Concerned,
        Default,
        Disbelief,
        Eating,
        Grimace,
        Sad,
        ScreamOpen,
        Serious,
        Smile,
        Tongue,
        Twinkle,
        Vomit
    }

    public enum SkinColor {
        Tanned,
        Yellow,
        Pale,
        Light,
        Brown,
        DarkBrown,
        Black
    }


    @Override
    public String getAvatarURL(User user) {
        return getRandom();
    }

    public String getRandom() {
        return getURL(AvatarStyle.Circle,
                getRandom(TopType.values()),
                getRandom(AccessoriesType.values()),
                getRandom(HatColor.values()),
                getRandom(HairColor.values()),
                getRandom(FacialHairType.values()),
                getRandom(FacialHairColor.values()),
                getRandom(ClotheType.values()),
                getRandom(ClotheColor.values()),
                getRandom(GraphicType.values()),
                getRandom(EyeType.values()),
                getRandom(EyebrowType.values()),
                getRandom(MouthType.values()),
                getRandom(SkinColor.values()));
    }

    public <T> T getRandom(T[] values) {
        List<T> elements = Collections.unmodifiableList(Arrays.asList(values));
        return elements.get(new Random().nextInt(elements.size()));
    }


    public String getURL(AvatarStyle style,
                         TopType topType,
                         AccessoriesType accessoriesType,
                         HatColor hatColor,
                         HairColor hairColor,
                         FacialHairType facialHairType,
                         FacialHairColor facialHairColor,
                         ClotheType clotheType,
                         ClotheColor clotheColor,
                         GraphicType graphicType,
                         EyeType eyeType,
                         EyebrowType eyebrowType,
                         MouthType mouthType,
                         SkinColor skinColor) {

        return "https://avataaars.io/?" +
                "avatarStyle=" +style.toString()+ "&" +
                "accessoriesType=" +accessoriesType.toString()+ "&" +
                "clotheColor="+clotheColor.toString()+"&" +
                "clotheType="+clotheType.toString()+"&" +
                "eyeType="+eyeType.toString()+"&" +
                "eyebrowType="+eyebrowType.toString()+"&" +
                "facialHairColor="+facialHairColor.toString()+"&" +
                "facialHairType="+facialHairType.toString()+"&" +
                "hairColor="+hairColor.toString()+"&" +
                "hatColor="+hatColor.toString()+"&" +
                "mouthType="+mouthType.toString()+"&" +
                "skinColor="+skinColor.toString()+"&" +
                "graphicType="+graphicType.toString()+"&" +
                "topType="+topType.toString();
    }



}
