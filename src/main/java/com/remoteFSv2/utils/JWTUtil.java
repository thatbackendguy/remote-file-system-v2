package com.remoteFSv2.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.remoteFSv2.server.Config;

import java.util.Date;

public class JWTUtil
{
    // generate JWT token valid for 1hr
    public static String generateToken(String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(Config.JWT_SECRET);
            String token = JWT.create()
                    .withIssuer("remoteFS")
                    .withSubject(username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + Config.JWT_EXPIRATION_TIME_MS))
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            System.out.println(Constants.SERVER + "JWT Token creation failed!");
        }
        return null;
    }

    // verify JWT token
    public static String verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(Config.JWT_SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("remoteFS")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException exception) {
            System.out.println(Constants.SERVER + "JWT Verification failed!");
        }
        return null;
    }
}
